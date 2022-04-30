import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar/navbar.component';
import { PageComponent } from './page/page.component';
import { ListingComponent } from './listing/listing.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PopupComponent } from './popup/popup.component';
import { NewContractComponent } from './new-contract/new-contract.component';
import { LoginAndCreateAccountComponent } from './popup/login-and-create-account/login-and-create-account.component';
import { MyPropertiesComponent } from './dashboard/my-properties/my-properties.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NewPropertyComponent } from './new-property/new-property.component';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    PageComponent,
    ListingComponent,
    DashboardComponent,
    PopupComponent,
    NewContractComponent,
    LoginAndCreateAccountComponent,
    MyPropertiesComponent,
    NewPropertyComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatRippleModule,
    MatIconModule,
    MatSelectModule
    ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
