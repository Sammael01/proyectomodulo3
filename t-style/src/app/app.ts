import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navcomponent } from './components/navcomponent/navcomponent';
import { Foodercomponent } from './components/foodercomponent/foodercomponent';
import { Toastcomponent } from './components/toastcomponent/toastcomponent';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navcomponent, Foodercomponent, Toastcomponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('T-Style');
}
