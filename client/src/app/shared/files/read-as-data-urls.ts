/** Reads image files as data URLs, the format the API's upload endpoints accept. */
export function readAsDataUrls(files: FileList | null): Promise<string[]> {
  return Promise.all(
    Array.from(files ?? []).map(
      (file) =>
        new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => resolve(reader.result as string);
          reader.onerror = () => reject(reader.error);
          reader.readAsDataURL(file);
        }),
    ),
  );
}
