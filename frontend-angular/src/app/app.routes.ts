import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './auth/guards/auth.guard';
import { LoginComponent } from './auth/components/login.component';
import { RegisterComponent } from './auth/components/register.component';
import { VerifyEmailComponent } from './auth/components/verify-email.component';
import { MfaSetupComponent } from './auth/components/mfa-setup.component';
import { MfaVerifyComponent } from './auth/components/mfa-verify.component';
import { SocialCallbackComponent } from './auth/components/social-callback.component';
import { ScoringComponent } from './scoring/scoring.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  { path: 'verify-email', component: VerifyEmailComponent, canActivate: [guestGuard] },
  { path: 'mfa/setup', component: MfaSetupComponent, canActivate: [guestGuard] },
  { path: 'mfa/verify', component: MfaVerifyComponent, canActivate: [guestGuard] },
  { path: 'auth/social-callback', component: SocialCallbackComponent, canActivate: [guestGuard] },
  { path: 'scoring', component: ScoringComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' }
];
