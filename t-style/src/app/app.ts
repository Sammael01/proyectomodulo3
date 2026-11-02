import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navcomponent } from './components/navcomponent/navcomponent';
import { Foodercomponent } from './components/foodercomponent/foodercomponent';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navcomponent, Foodercomponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('T-Style');
}
