import { HttpClient } from '@angular/common/http';
import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL, joinApiUrl } from './api-config';
import { API_ENDPOINTS } from './api-endpoints';
import {
  AuthResponse,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
  UserProfile,
  VerifyEmailRequest,
} from './models';
import { readStorage, writeStorage } from './storage';

const SESSION_KEY = 'tstyle_session';
/** Máximo admitido por setTimeout (~24,8 días). */
const MAX_TIMEOUT = 2_147_483_647;

/** Lee el claim `exp` (segundos) del JWT. `null` si el token no tiene `exp`; `undefined` si es ilegible. */
export function jwtExpiration(token: string): number | null | undefined {
  const parts = typeof token === 'string' ? token.split('.') : [];
  if (parts.length !== 3) return undefined;
  try {
    const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    const payload = JSON.parse(json);
    return typeof payload?.exp === 'number' ? payload.exp : null;
  } catch {
    return undefined;
  }
}

/**
 * Un token ilegible se considera inválido. Un token sin `exp` se acepta en el cliente:
 * la validez definitiva siempre la decide Spring Security (responderá 401).
 */
export function tokenExpired(token: string, now = Date.now()): boolean {
  const exp = jwtExpiration(token);
  if (exp === undefined) return true;
  return exp !== null && exp * 1000 <= now;
}

function isAuthResponse(value: unknown): value is AuthResponse {
  const v = value as Partial<AuthResponse> | null;
  return !!v && typeof v.token === 'string' && typeof v.name === 'string' && typeof v.email === 'string' && typeof v.role === 'string';
}

function initialSession(): AuthResponse | null {
  const session = readStorage<unknown>(SESSION_KEY, null);
  if (!isAuthResponse(session) || tokenExpired(session.token)) {
    writeStorage(SESSION_KEY, null);
    return null;
  }
  return session;
}

/**
 * Sesión del usuario. El rol guardado en el navegador solo se usa para la interfaz
 * (mostrar/ocultar opciones); la autorización real la realiza el backend con el JWT.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly api = inject(API_BASE_URL);
  private readonly routes = API_ENDPOINTS.auth;
  private readonly session = signal<AuthResponse | null>(initialSession());
  private expiryTimer: ReturnType<typeof setTimeout> | undefined;

  readonly user = this.session.asReadonly();
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');
  readonly firstName = computed(() => this.session()?.name?.split(' ')[0] ?? '');

  constructor() {
    this.scheduleExpiry(this.session());
    inject(DestroyRef).onDestroy(() => clearTimeout(this.expiryTimer));
  }

  token(): string | null {
    const session = this.session();
    if (session && tokenExpired(session.token)) {
      this.clear();
      return null;
    }
    return session?.token ?? null;
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.url(this.routes.login), body).pipe(tap((response) => this.store(response)));
  }

  register(body: RegisterRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.url(this.routes.register), body);
  }

  verifyEmail(body: VerifyEmailRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.url(this.routes.verifyEmail), body);
  }

  resendVerification(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.url(this.routes.resendVerification), { email });
  }

  forgotPassword(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.url(this.routes.forgotPassword), { email });
  }

  resetPassword(body: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.url(this.routes.resetPassword), body);
  }

  /** Consulta el perfil al backend y actualiza nombre/correo/rol de la sesión con los datos del servidor. */
  me(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.url(this.routes.me)).pipe(
      tap((profile) => {
        const current = this.session();
        if (!current) return;
        const updated = { ...current, ...profile, token: current.token };
        this.session.set(updated);
        writeStorage(SESSION_KEY, updated);
      })
    );
  }

  logout(redirect = true): void {
    this.clear();
    if (redirect) this.router.navigateByUrl('/');
  }

  private url(path: string): string {
    return joinApiUrl(this.api, path);
  }

  private store(response: AuthResponse): void {
    this.session.set(response);
    writeStorage(SESSION_KEY, response);
    this.scheduleExpiry(response);
  }

  private clear(): void {
    clearTimeout(this.expiryTimer);
    this.session.set(null);
    writeStorage(SESSION_KEY, null);
  }

  /** Cierra la sesión automáticamente cuando el JWT expira con la página abierta. */
  private scheduleExpiry(session: AuthResponse | null): void {
    clearTimeout(this.expiryTimer);
    const exp = session ? jwtExpiration(session.token) : null;
    if (typeof exp !== 'number') return;
    const delay = exp * 1000 - Date.now();
    if (delay > MAX_TIMEOUT) return;
    this.expiryTimer = setTimeout(() => {
      if (this.session()?.token !== session?.token) return;
      this.clear();
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url, expired: 1 } });
    }, Math.max(0, delay));
  }
}
