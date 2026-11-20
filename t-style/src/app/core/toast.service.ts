import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  text: string;
  kind: 'ok' | 'error';
  leaving: boolean;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  show(text: string, kind: Toast['kind'] = 'ok'): void {
    const id = ++this.seq;
    this.toasts.update((list) => [...list.slice(-2), { id, text, kind, leaving: false }]);
    setTimeout(() => this.dismiss(id), 3000);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.map((t) => (t.id === id ? { ...t, leaving: true } : t)));
    setTimeout(() => this.toasts.update((list) => list.filter((t) => t.id !== id)), 450);
  }
}
