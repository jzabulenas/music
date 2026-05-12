import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LikedArtist } from './liked-artist.model';

@Injectable({ providedIn: 'root' })
export class ArtistService {
  private readonly http = inject(HttpClient);

  readonly artists = signal<LikedArtist[]>([]);

  load(): Observable<LikedArtist[]> {
    return this.http
      .get<LikedArtist[]>('/api/v1/liked-artists')
      .pipe(tap((artists) => this.artists.set(artists)));
  }

  add(name: string): Observable<LikedArtist> {
    return this.http
      .post<LikedArtist>('/api/v1/liked-artists', { name })
      .pipe(tap((artist) => this.artists.update((list) => [...list, artist])));
  }

  remove(id: number): Observable<void> {
    return this.http
      .delete<void>(`/api/v1/liked-artists/${id}`)
      .pipe(tap(() => this.artists.update((list) => list.filter((a) => a.id !== id))));
  }
}
