import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SharedModule } from 'src/app/shared/shared.module';
import { FlatComponent } from './flat/flat';

@NgModule({
  imports: [
    BrowserModule,
    SharedModule,
  ],
  entryComponents: [
    FlatComponent,
  ],
  declarations: [
    FlatComponent,
  ],
  exports: [
    FlatComponent,
  ],
})
export class Controller_Ess_ControllbyExternal { }
