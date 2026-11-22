/** Genera un JWT sin firma válida, solo para pruebas del cliente. */
export function fakeJwt(payload: Record<string, unknown>): string {
  const enc = (v: unknown) => btoa(JSON.stringify(v)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `${enc({ alg: 'HS256', typ: 'JWT' })}.${enc(payload)}.firma`;
}

export const validToken = () => fakeJwt({ sub: 'ana@t-style.co', exp: Math.floor(Date.now() / 1000) + 3600 });
export const expiredToken = () => fakeJwt({ sub: 'ana@t-style.co', exp: Math.floor(Date.now() / 1000) - 60 });
