export interface Recommendation {
  id: number;
  name: string;
  genre: string | null;
  reason: string | null;
}

// `Default` asks the backend for unconstrained suggestions; every other value is sent as the
// genre the AI must stick to. Keep in sync with `RecommendationGenre` on the backend.
export const RECOMMENDATION_GENRES = [
  'Default',
  'Pop',
  'Rap',
  'Rock',
  'Dance',
  'Metal',
  'Electronic',
  'R&B',
  'Country',
  'Jazz',
] as const;

export type RecommendationGenre = (typeof RECOMMENDATION_GENRES)[number];
