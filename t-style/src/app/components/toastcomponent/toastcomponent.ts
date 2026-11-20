import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-toastcomponent',
  templateUrl: './toastcomponent.html',
  styleUrl: './toastcomponent.css',
})
export class Toastcomponent {
  protected readonly toast = inject(ToastService);
}
