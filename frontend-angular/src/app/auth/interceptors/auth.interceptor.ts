import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import {
  catchError,
  switchMap,
  throwError
} from 'rxjs';
import { TokenStoreService } from '../services/token-store.service';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStore = inject(TokenStoreService);
  const auth = inject(AuthService);

  const token = tokenStore.get();
  const isAuthEndpoint =
    request.url.includes('/api/v1/auth/');

  const securedRequest =
    token && !isAuthEndpoint
      ? request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      : request;

  return next(securedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || isAuthEndpoint) {
        return throwError(() => error);
      }

      /*
       * Access JWT vencido:
       * usa la cookie HttpOnly de refresh, recibe un JWT nuevo
       * y reintenta una sola vez la petición original.
       */
      return auth.refresh().pipe(
        switchMap(result =>
          next(
            request.clone({
              setHeaders: {
                Authorization:
                  `Bearer ${result.accessToken}`
              }
            })
          )
        ),
        catchError(refreshError => {
          tokenStore.clear();
          return throwError(() => refreshError);
        })
      );
    })
  );
};
