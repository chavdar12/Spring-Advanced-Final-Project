export interface Page<T> {
  content: T[];
  total: number;
}

export interface AdSummary {
  id: number;
  title: string;
  imageUrl: string | null;
  price: number;
  condition: string;
  description: string;
  views: number;
  creator: string;
  likedBy: string[];
}

export interface Seller {
  username: string;
  firstName: string;
  lastName: string;
  aboutMe: string;
  photoUrl: string;
}

export interface AdDetails {
  id: number;
  title: string;
  images: {id: number; url: string}[];
  description: string;
  price: number;
  condition: string;
  delivery: string;
  views: number;
  createdOn: string;
  categoryId: number;
  categoryName: string;
  seller: Seller;
}

/** Editable fields of an advertisement. */
export interface AdForm {
  title: string;
  description: string;
  price: number;
  condition: string;
  delivery: string;
  categoryId: number;
}

export interface AdOptions {
  conditions: string[];
  deliveries: string[];
}

/** UI search state; "All" and "none" mean "no filter". */
export interface AdSearch {
  search: string;
  category: string;
  condition: string;
  min: number;
  max: number;
  page: number;
  sortBy: string;
  order: 'ASC' | 'DESC';
}

export const PAGE_SIZE = 6; // fixed by the API

export const PLACEHOLDER_IMAGE =
  'https://res.cloudinary.com/knight-cloud/image/upload/v1586614999/xtxuiw3kqqy5wxq6ufla.png';
