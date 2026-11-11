import {AdSummary} from '../advertisements/advertisement';

export interface Category {
  id: number;
  name: string;
  photoUrl: string;
}

export interface TopCategory {
  name: string;
  photoUrl: string;
  advertisements: AdSummary[];
}
