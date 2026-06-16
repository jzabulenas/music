import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { SavedArtist } from './saved-artist.model';

@Injectable({ providedIn: 'root' })
export class SavedArtistService {
  private readonly http = inject(HttpClient);

  readonly savedArtists = signal<SavedArtist[]>([]);

  load(): Observable<SavedArtist[]> {
    return this.http
      .get<SavedArtist[]>('/api/v1/saved-artists')
      .pipe(tap((artists) => this.savedArtists.set(artists)));
  }

  save(name: string, genre: string | null): Observable<SavedArtist> {
    return this.http
      .post<SavedArtist>('/api/v1/saved-artists', { name, genre })
      .pipe(tap((artist) => this.savedArtists.update((list) => [...list, artist])));
  }

  remove(id: number): Observable<void> {
    return this.http
      .delete<void>(`/api/v1/saved-artists/${id}`)
      .pipe(tap(() => this.savedArtists.update((list) => list.filter((a) => a.id !== id))));
  }
}
