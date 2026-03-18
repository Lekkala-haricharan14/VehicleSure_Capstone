import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject, Injector } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const injector = inject(Injector);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        let serverError = error.error;
        
        // Handle cases where responseType is 'text' but body is JSON
        if (typeof serverError === 'string' && serverError.trim()) {
          try {
            serverError = JSON.parse(serverError);
          } catch (e) {
            // Not JSON, use the string itself if it's not empty
            errorMessage = serverError;
          }
        }

        if (serverError && serverError.message) {
          errorMessage = serverError.message;
        } else if (typeof serverError === 'string' && serverError.trim()) {
           errorMessage = serverError;
        } else {
          switch (error.status) {
            case 400:
              errorMessage = 'Bad Request';
              break;
            case 401:
              errorMessage = 'Unauthorized. Please login again.';
              break;
            case 403:
              errorMessage = 'Forbidden. You do not have permission to access this resource.';
              break;
            case 404:
              errorMessage = 'Resource not found.';
              break;
            case 500:
              errorMessage = 'Internal Server Error. Please try again later.';
              break;
          }
        }
      }

      console.error(`Status: ${error.status}\nMessage: ${errorMessage}`);
      

      if (error.status === 401) {
        localStorage.removeItem('token');
        router.navigate(['/login']);
      }

      
      const toastService = injector.get(ToastService);
      toastService.error(errorMessage);
      
      return throwError(() => new Error(errorMessage));
    })
  );
};
