import {HttpErrorResponse} from '@angular/common/http';

/** RFC 9457 error body returned by every failing API call. */
export interface Problem {
  title: string;
  status: number;
  detail?: string;
  errors?: Record<string, string>;
}

/** Field errors first, then the problem detail, then a generic fallback. */
export function errorMessage(error: unknown): string {
  const response = error as Partial<HttpErrorResponse> | null;
  const body = response?.error;
  const problem: Partial<Problem> | null = body && typeof body === 'object' ? body : null;
  const fields = Object.entries(problem?.errors ?? {}).map(
    ([field, message]) => `${field} ${message}`,
  );
  if (fields.length) {
    return fields.join(', ');
  }
  if (problem?.detail) {
    return problem.detail;
  }
  return response?.status === 429 ? 'Too many requests, slow down.' : 'Something went wrong.';
}
