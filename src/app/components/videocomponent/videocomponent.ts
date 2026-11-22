import { Component, ElementRef, WritableSignal, computed, inject, input, linkedSignal, signal, viewChild } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { CatalogService } from '../../core/catalog.service';
import { Product, Video, VideoComment } from '../../core/models';
import { MoneyPipe } from '../../core/money.pipe';
import { readStorage, writeStorage } from '../../core/storage';
import { ToastService } from '../../core/toast.service';
import { VideoService } from '../../core/video.service';

const SOCIAL_KEY = 'tstyle_video_social';

interface SocialState {
  liked: string[];
  saved: string[];
  comments: Record<string, VideoComment[]>;
}

@Component({
  selector: 'app-videocomponent',
  imports: [DecimalPipe, RouterLink, MoneyPipe],
  templateUrl: './videocomponent.html',
  styleUrl: './videocomponent.css',
})
export class Videocomponent {
  protected readonly cart = inject(CartService);
  protected readonly auth = inject(AuthService);
  protected readonly videoService = inject(VideoService);
  private readonly catalog = inject(CatalogService);
  private readonly toast = inject(ToastService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly router = inject(Router);
  private readonly videoEl = viewChild<ElementRef<HTMLVideoElement>>('player');
  private readonly frameEl = viewChild<ElementRef<HTMLElement>>('frame');

  readonly v = input<string>();

  private readonly social = readStorage<SocialState>(SOCIAL_KEY, {
    liked: [],
    saved: [],
    comments: {
      v1: [
        { user: 'Laura M.', text: '¡Necesito ese hoodie ya! 🔥', time: 'Hace 3 h' },
        { user: 'Andrés P.', text: 'Los colores de esta temporada están muy buenos.', time: 'Hace 1 día' },
      ],
    },
  });

  protected readonly videos = this.videoService.videos;
  protected readonly categories = computed(() => ['Todos', ...new Set(this.videos().map((x) => x.category))]);
  protected readonly category = signal('Todos');
  protected readonly query = signal('');
  protected readonly current = linkedSignal<Video>(() => {
    const list = this.videos();
    return list.find((x) => x.id === this.v()) ?? list[0];
  });

  protected readonly playing = signal(false);
  protected readonly started = signal(false);
  protected readonly time = signal(0);
  protected readonly duration = signal(0);
  protected readonly volume = signal(0.8);
  protected readonly muted = signal(false);
  protected readonly autoplay = signal(true);
  protected readonly progress = computed(() => (this.duration() ? (this.time() / this.duration()) * 100 : 0));

  protected readonly liked = signal<Set<string>>(new Set(this.social.liked));
  protected readonly saved = signal<Set<string>>(new Set(this.social.saved));
  protected readonly comments = signal<Record<string, VideoComment[]>>(this.social.comments);
  protected readonly newComment = signal('');

  protected readonly playlist = computed(() => {
    const q = this.query().trim().toLowerCase();
    return this.videos().filter(
      (x) => (this.category() === 'Todos' || x.category === this.category()) && (!q || x.title.toLowerCase().includes(q))
    );
  });
  protected readonly look = computed<Product[]>(() =>
    this.current()
      .productIds.map((id) => this.catalog.byId(id))
      .filter((p): p is Product => !!p)
  );
  protected readonly currentComments = computed(() => this.comments()[this.current().id] ?? []);
  protected readonly isLiked = computed(() => this.liked().has(this.current().id));
  protected readonly isSaved = computed(() => this.saved().has(this.current().id));
  protected readonly embedUrl = computed<SafeResourceUrl | null>(() => {
    const id = this.current().youtubeId;
    return id
      ? this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube-nocookie.com/embed/${id}?autoplay=${this.started() ? 1 : 0}&rel=0`)
      : null;
  });

  protected select(video: Video, play = true): void {
    if (video.id === this.current().id) {
      if (!video.youtubeId) this.toggle();
      return;
    }
    this.current.set(video);
    this.time.set(0);
    this.duration.set(0);
    this.started.set(play);
    this.router.navigate([], { queryParams: { v: video.id }, replaceUrl: true });
    window.scrollTo({ top: 0, behavior: 'smooth' });
    if (video.youtubeId) return;
    queueMicrotask(() => {
      const el = this.videoEl()?.nativeElement;
      if (!el) return;
      el.load();
      if (play) el.play().catch(() => this.playing.set(false));
    });
  }

  protected start(): void {
    this.started.set(true);
    if (!this.current().youtubeId) queueMicrotask(() => this.toggle());
  }

  protected toggle(): void {
    const el = this.videoEl()?.nativeElement;
    if (!el) return;
    this.started.set(true);
    if (el.paused) el.play().catch(() => this.playing.set(false));
    else el.pause();
  }

  protected seek(ev: MouseEvent): void {
    const el = this.videoEl()?.nativeElement;
    const bar = ev.currentTarget as HTMLElement;
    if (!el || !this.duration()) return;
    const ratio = (ev.clientX - bar.getBoundingClientRect().left) / bar.offsetWidth;
    el.currentTime = Math.max(0, Math.min(1, ratio)) * this.duration();
  }

  protected skip(sec: number): void {
    const el = this.videoEl()?.nativeElement;
    if (el) el.currentTime = Math.max(0, Math.min(el.duration || 0, el.currentTime + sec));
  }

  protected setVolume(value: string): void {
    const el = this.videoEl()?.nativeElement;
    const level = Number(value);
    this.volume.set(level);
    this.muted.set(level === 0);
    if (el) {
      el.volume = level;
      el.muted = level === 0;
    }
  }

  protected toggleMute(): void {
    const el = this.videoEl()?.nativeElement;
    this.muted.update((m) => !m);
    if (el) el.muted = this.muted();
  }

  protected fullscreen(): void {
    const frame = this.frameEl()?.nativeElement;
    if (!frame) return;
    if (document.fullscreenElement) document.exitFullscreen();
    else frame.requestFullscreen?.();
  }

  protected onMeta(el: HTMLVideoElement): void {
    this.duration.set(el.duration || 0);
    el.volume = this.volume();
    el.muted = this.muted();
  }

  protected onEnded(): void {
    this.playing.set(false);
    if (!this.autoplay()) return;
    const list = this.playlist();
    const next = list[(list.findIndex((x) => x.id === this.current().id) + 1) % list.length];
    if (next && next.id !== this.current().id) this.select(next);
  }

  protected format(sec: number): string {
    if (!isFinite(sec)) return '0:00';
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  protected toggleIn(set: WritableSignal<Set<string>>, label: string): void {
    const id = this.current().id;
    let added = false;
    set.update((s) => {
      const n = new Set(s);
      if (n.has(id)) n.delete(id);
      else {
        n.add(id);
        added = true;
      }
      return n;
    });
    this.persist();
    if (added && label) this.toast.show(label);
  }

  protected share(): void {
    const url = `${location.origin}/videos?v=${this.current().id}`;
    navigator.clipboard?.writeText(url).then(
      () => this.toast.show('Enlace copiado al portapapeles'),
      () => this.toast.show('No fue posible copiar el enlace', 'error')
    );
  }

  protected addComment(): void {
    const text = this.newComment().trim();
    if (!text) return;
    const id = this.current().id;
    const user = this.auth.user()?.name ?? 'Invitado';
    this.comments.update((c) => ({ ...c, [id]: [{ user, text, time: 'Ahora' }, ...(c[id] ?? [])] }));
    this.newComment.set('');
    this.persist();
  }

  protected addToCart(p: Product): void {
    this.cart.add(p);
    this.toast.show(`${p.name} agregado al carrito`);
  }

  private persist(): void {
    writeStorage(SOCIAL_KEY, { liked: [...this.liked()], saved: [...this.saved()], comments: this.comments() });
  }
}
