import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent),
  },
  // Activates when `return '/index.html';` gets called in `proxy.conf.js`
  {
    path: 'login/ott',
    loadComponent: () => import('./login/login-ott.component').then((m) => m.LoginOttComponent),
  },
  {
    path: 'app',
    loadComponent: () => import('./shell/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'artists',
        loadComponent: () =>
          import('./artist/artists-page.component').then((m) => m.ArtistsPageComponent),
      },
      {
        path: 'recommendations',
        loadComponent: () =>
          import('./recommendation/recommendations-page.component').then(
            (m) => m.RecommendationsPageComponent,
          ),
      },
      {
        path: 'saved',
        loadComponent: () =>
          import('./saved/saved-artists-page.component').then((m) => m.SavedArtistsPageComponent),
      },
      { path: '', redirectTo: 'artists', pathMatch: 'full' },
      { path: '**', redirectTo: 'artists' },
    ],
  },
  { path: '', redirectTo: '/app/artists', pathMatch: 'full' },
];
