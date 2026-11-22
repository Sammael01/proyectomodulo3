import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { map, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Video } from './models';

const SRC = {
  flower: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
  friday: 'https://interactive-examples.mdn.mozilla.net/media/examples/friday.mp4',
  bunny: 'https://www.w3schools.com/html/mov_bbb.mp4',
};

const gradient = (a: string, b: string) => `linear-gradient(135deg, ${a} 0%, ${b} 100%)`;

const LOCAL_VIDEOS: Video[] = [
  { id: 'v1', title: 'Lookbook Otoño 2026', category: 'Lookbook', src: SRC.flower, poster: gradient('#181919', '#5d6812'), views: 48210, likes: 3120, date: 'Hace 2 días', productIds: ['p1', 'p2'],
    description: 'La nueva colección en movimiento: capas, tonos tierra y siluetas oversize para la temporada.' },
  { id: 'v2', title: 'Backstage: sesión de fotos', category: 'Backstage', src: SRC.friday, poster: gradient('#0e1010', '#ff5b22'), views: 12890, likes: 940, date: 'Hace 5 días', productIds: ['p3', 'p5'],
    description: 'Detrás de cámaras de la campaña. Así se construye cada look antes de llegar a la tienda.' },
  { id: 'v3', title: '3 formas de usar el cargo', category: 'Estilo', src: SRC.bunny, poster: gradient('#2c2e2d', '#e8f20c'), views: 30544, likes: 2210, date: 'Hace 1 semana', productIds: ['p2', 'p4'],
    description: 'Del día a la noche con una sola prenda: combinaciones fáciles con el Cargo Utility Pant.' },
  { id: 'v4', title: 'Streetwear en la ciudad', category: 'Lookbook', src: SRC.bunny, poster: gradient('#353836', '#ff5b22'), views: 9870, likes: 610, date: 'Hace 2 semanas', productIds: ['p4', 'p8'],
    description: 'Recorremos la ciudad con la Chaqueta Shell Neon como protagonista.' },
  { id: 'v5', title: 'Cómo elegir tu talla', category: 'Estilo', src: SRC.flower, poster: gradient('#181919', '#dedcd5'), views: 21003, likes: 1450, date: 'Hace 3 semanas', productIds: ['p1', 'p3', 'p9'],
    description: 'Guía rápida de fit: regular, boxy y oversize. Encuentra el corte ideal para ti.' },
  { id: 'v6', title: 'Unboxing T-Style', category: 'Backstage', src: SRC.friday, poster: gradient('#0e1010', '#e8f20c'), views: 6540, likes: 480, date: 'Hace 1 mes', productIds: ['p5', 'p10', 'p12'],
    description: 'Así llega tu pedido: empaque, detalles y sorpresas en cada caja.' },
];

const PRODUCT_ROTATION = [['p1', 'p2'], ['p3', 'p5'], ['p4', 'p8'], ['p6', 'p9'], ['p7', 'p10'], ['p11', 'p12']];

interface YtSearch {
  items: { id: { videoId: string } }[];
}

interface YtVideos {
  items: {
    id: string;
    snippet: { title: string; description: string; channelTitle: string; publishedAt: string; thumbnails: Record<string, { url: string }> };
    statistics?: { viewCount?: string; likeCount?: string };
  }[];
}

@Injectable({ providedIn: 'root' })
export class VideoService {
  private readonly http = new HttpClient(inject(HttpBackend));

  readonly videos = signal<Video[]>(LOCAL_VIDEOS);
  readonly source = signal<'local' | 'youtube'>('local');
  readonly loading = signal(false);

  constructor() {
    if (environment.youtubeApiKey) this.loadYoutube(environment.youtubeQuery);
  }

  loadYoutube(query: string): void {
    const key = environment.youtubeApiKey;
    if (!key) return;
    this.loading.set(true);
    const base = 'https://www.googleapis.com/youtube/v3';
    this.http
      .get<YtSearch>(`${base}/search`, { params: { part: 'snippet', type: 'video', maxResults: 12, q: query, videoEmbeddable: 'true', key } })
      .pipe(
        switchMap((search) => {
          const ids = search.items.map((i) => i.id.videoId).filter(Boolean).join(',');
          return ids
            ? this.http.get<YtVideos>(`${base}/videos`, { params: { part: 'snippet,statistics', id: ids, key } })
            : of({ items: [] } as YtVideos);
        }),
        map((res) =>
          res.items.map<Video>((item, index) => ({
            id: item.id,
            youtubeId: item.id,
            title: item.snippet.title,
            description: item.snippet.description,
            channel: item.snippet.channelTitle,
            category: 'YouTube',
            thumbnail: (item.snippet.thumbnails['high'] ?? item.snippet.thumbnails['default'])?.url,
            poster: gradient('#181919', '#2c2e2d'),
            views: Number(item.statistics?.viewCount ?? 0),
            likes: Number(item.statistics?.likeCount ?? 0),
            date: new Date(item.snippet.publishedAt).toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' }),
            productIds: PRODUCT_ROTATION[index % PRODUCT_ROTATION.length],
          }))
        )
      )
      .subscribe({
        next: (videos) => {
          if (videos.length) {
            this.videos.set(videos);
            this.source.set('youtube');
          }
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
