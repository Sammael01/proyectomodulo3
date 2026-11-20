import { Pipe, PipeTransform, inject } from '@angular/core';
import { CurrencyService } from './currency.service';

@Pipe({ name: 'money', pure: false })
export class MoneyPipe implements PipeTransform {
  private readonly currency = inject(CurrencyService);

  transform(value: number | null | undefined): string {
    return this.currency.format(value ?? 0);
  }
}
