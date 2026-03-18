import { inject } from '@angular/core';
import { HttpRequest, HttpHandlerFn, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export function jwtInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    const authService = inject(AuthService);
    const token = authService.token();

    let cloned = req;
    if (token) {
        cloned = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
    }

    return next(cloned).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 || error.status === 403) {
                console.error("JWT ERROR CATCHED:", error.message, error);

            }
            return throwError(() => error);
        })
    );
}
