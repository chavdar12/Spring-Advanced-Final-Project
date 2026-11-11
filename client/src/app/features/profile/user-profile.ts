import {AdSummary} from '../advertisements/advertisement';
import {Town} from '../regions/region';

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  town: Town | null;
  aboutMe: string | null;
  completed: boolean;
  photoUrl: string | null;
  roles: string[];
  subscribedTo: string[];
  createdAdvertisements: AdSummary[];
  favorites: AdSummary[];
}

export interface ProfileInfo {
  firstName: string;
  lastName: string;
  townId: number;
  aboutMe: string;
}
