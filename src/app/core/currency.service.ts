import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { readStorage, writeStorage } from './storage';

export type CurrencyCode = 'COP' | 'USD' | 'EUR';

const CURRENCY_KEY = 'tstyle_currency';
const RATES_URL = 'https://open.er-api.com/v6/latest/COP';
const FALLBACK_RATES: Record<CurrencyCode, number> = { COP: 1, USD: 0.00025, EUR: 0.00023 };

@Injectable({ providedIn: 'root' })
export class CurrencyService {
  private readonly http = new HttpClient(inject(HttpBackend));
  private readonly rates = signal<Record<CurrencyCode, number>>(FALLBACK_RATES);

  readonly codes: CurrencyCode[] = ['COP', 'USD', 'EUR'];
  readonly current = signal<CurrencyCode>(readStorage(CURRENCY_KEY, 'COP'));
  readonly live = signal(false);
  readonly updatedAt = signal<string | null>(null);

  constructor() {
    this.http.get<{ result: string; rates: Record<string, number>; time_last_update_utc: string }>(RATES_URL).subscribe({
      next: (res) => {
        if (res.result !== 'success') return;
        this.rates.set({ COP: 1, USD: res.rates['USD'] ?? FALLBACK_RATES.USD, EUR: res.rates['EUR'] ?? FALLBACK_RATES.EUR });
        this.live.set(true);
        this.updatedAt.set(res.time_last_update_utc);
      },
      error: () => this.live.set(false),
    });
  }

  set(code: CurrencyCode): void {
    this.current.set(code);
    writeStorage(CURRENCY_KEY, code);
  }

  format(valueCop: number): string {
    const code = this.current();
    const value = valueCop * this.rates()[code];
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: code,
      maximumFractionDigits: code === 'COP' ? 0 : 2,
      minimumFractionDigits: code === 'COP' ? 0 : 2,
    }).format(value);
  }
}
