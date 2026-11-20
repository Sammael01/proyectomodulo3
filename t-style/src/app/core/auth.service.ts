import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
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

function tokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now();
  } catch {
    return false;
  }
}

function initialSession(): AuthResponse | null {
  const session = readStorage<AuthResponse | null>(SESSION_KEY, null);
  if (session && tokenExpired(session.token)) {
    writeStorage(SESSION_KEY, null);
    return null;
  }
  return session;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly api = environment.apiUrl;
  private readonly session = signal<AuthResponse | null>(initialSession());

  readonly user = this.session.asReadonly();
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');
  readonly firstName = computed(() => this.session()?.name.split(' ')[0] ?? '');

  token(): string | null {
    const session = this.session();
    if (session && tokenExpired(session.token)) {
      this.clear();
      return null;
    }
    return session?.token ?? null;
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, body).pipe(
      tap((response) => {
        this.session.set(response);
        writeStorage(SESSION_KEY, response);
      })
    );
  }

  register(body: RegisterRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.api}/users/register`, body);
  }

  verifyEmail(body: VerifyEmailRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.api}/users/verify-email`, body);
  }

  resendVerification(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.api}/users/resend-verification`, { email });
  }

  forgotPassword(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.api}/auth/forgot-password`, { email });
  }

  resetPassword(body: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.api}/auth/reset-password`, body);
  }

  me(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.api}/auth/me`).pipe(
      tap((profile) => {
        const current = this.session();
        if (!current) return;
        const updated = { ...current, ...profile };
        this.session.set(updated);
        writeStorage(SESSION_KEY, updated);
      })
    );
  }

  logout(redirect = true): void {
    this.clear();
    if (redirect) this.router.navigateByUrl('/');
  }

  private clear(): void {
    this.session.set(null);
    writeStorage(SESSION_KEY, null);
  }
}
