import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CartService } from './cart.service';
import { CatalogService } from './catalog.service';
import { OrderService } from './order.service';

describe('Servicios en modo local (endpoints pendientes)', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
  });
  afterEach(() => localStorage.clear());

  it('no realizan peticiones HTTP a rutas supuestas y conservan los datos de demostración', async () => {
    const catalog = TestBed.inject(CatalogService);
    const cart = TestBed.inject(CartService);
    const orders = TestBed.inject(OrderService);
    expect(catalog.source).toBe('local');
    expect(catalog.products().length).toBe(12);
    cart.add(catalog.products()[0], 'M', 2);
    const order = await firstValueFrom(orders.submit({ fullName: 'Ana', phone: '3001234567', address: 'Calle 1 # 2-3', city: 'Bogotá', department: 'Bogotá D.C.', notes: '' }, 'cod'));
    expect(order.items[0].qty).toBe(2);
    expect(cart.items().length).toBe(0);
    TestBed.inject(HttpTestingController).verify();
  });
});
