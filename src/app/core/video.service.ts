import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { map, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { API_BASE_URL, joinApiUrl } from './api-config';
import { API_ENDPOINTS } from './api-endpoints';
import { apiMessage } from './api-error';
import { Video } from './models';

const SRC = {
  flower: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
  friday: 'https://interactive-examples.mdn.mozilla.net/media/examples/friday.mp4',
  bunny: 'https://www.w3schools.com/html/mov_bbb.mp4',
};

const gradient = (a: string, b: string) => `linear-gradient(135deg, ${a} 0%, ${b} 100%)`;

/** Videos de DEMOSTRACIÓN (no son contenido real). Se reemplazan cuando exista la fuente definitiva. */
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

/**
 * ADAPTADOR PENDIENTE: convierte el DTO de películas/trailers (TMDB vía backend del módulo B)
 * al modelo `Video`. Ajustar cuando B publique el DTO definitivo.
 */
export function toVideo(raw: unknown): Video {
  const dto = raw as Partial<Video> & { id: string | number };
  return {
    id: String(dto.id),
    title: dto.title ?? '',
    category: dto.category ?? 'Trailer',
    description: dto.description ?? '',
    poster: dto.poster ?? gradient('#181919', '#2c2e2d'),
    thumbnail: dto.thumbnail,
    src: dto.src,
    youtubeId: dto.youtubeId,
    channel: dto.channel,
    views: dto.views ?? 0,
    likes: dto.likes ?? 0,
    date: dto.date ?? '',
    productIds: dto.productIds ?? [],
  };
}

/**
 * Fuente de videos, por prioridad:
 *  1. Backend (módulo B, TMDB) cuando `API_ENDPOINTS.videos.list` esté confirmado.
 *     El token de TMDB vive SOLO en el backend; el frontend nunca lo recibe.
 *  2. YouTube Data API, si hay una clave PÚBLICA de navegador restringida por dominio y API.
 *  3. Videos de demostración locales.
 */
@Injectable({ providedIn: 'root' })
export class VideoService {
  /** Cliente sin interceptores: las peticiones a YouTube nunca llevan el JWT. */
  private readonly http = new HttpClient(inject(HttpBackend));
  /** Cliente con interceptores para el backend propio. */
  private readonly apiHttp = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);

  readonly videos = signal<Video[]>(LOCAL_VIDEOS);
  readonly source = signal<'local' | 'youtube' | 'api'>('local');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    if (API_ENDPOINTS.videos.list) this.loadFromBackend();
    else if (environment.youtubeApiKey) this.loadYoutube(environment.youtubeQuery);
  }

  /** Consume el servicio definitivo de videos del backend. Si falla, se muestra el error y se conservan los videos actuales. */
  loadFromBackend(): void {
    const route = API_ENDPOINTS.videos.list;
    if (!route) return;
    this.loading.set(true);
    this.error.set(null);
    this.apiHttp.get<unknown>(joinApiUrl(this.api, route)).subscribe({
      next: (body) => {
        const list = Array.isArray(body) ? body : ((body as { content?: unknown[] })?.content ?? []);
        const videos = list.map(toVideo);
        if (videos.length) {
          this.videos.set(videos);
          this.source.set('api');
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(apiMessage(err, 'No fue posible cargar los videos.'));
        this.loading.set(false);
      },
    });
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
        error: (err) => {
          this.error.set(apiMessage(err, 'No fue posible cargar los videos de YouTube.'));
          this.loading.set(false);
        },
      });
  }
}
