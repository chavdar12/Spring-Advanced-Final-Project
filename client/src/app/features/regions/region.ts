export interface Region {
  name: string;
  totalAds: number;
  totalUsers: number;
}

export interface Town {
  id: number;
  name: string;
  regionName: string;
}

export interface TownInRegion {
  id: number;
  name: string;
  advertisementsCount: number;
}
