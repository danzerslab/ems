import { Component } from '@angular/core';
import { AbstractFlatWidget } from 'src/app/shared/genericComponents/flat/abstract-flat-widget';
import { DefaultTypes } from 'src/app/shared/service/defaulttypes';
import { ChannelAddress, CurrentData, Utils } from 'src/app/shared/shared';

import { ModalComponent } from '../modal/modal';

@Component({
  selector: 'Controller_Ess_ConstantCurrent',
  templateUrl: './flat.html',
})
export class FlatComponent extends AbstractFlatWidget {

  public readonly CONVERT_MILLIAMPERE_TO_AMPERE = Utils.CONVERT_MILLIAMPERE_TO_AMPERE;
  public readonly CONVERT_MANUAL_ON_OFF = Utils.CONVERT_MANUAL_ON_OFF(this.translate);

  public chargeDischargeCurrent: { name: string, value: number };
  public propertyMode: DefaultTypes.ManualOnOff = null;

  protected override getChannelAddresses(): ChannelAddress[] {
    return [
      new ChannelAddress(this.component.id, "_PropertyCurrent"),
      new ChannelAddress(this.component.id, "_PropertyMode"),
    ];
  }

  protected override onCurrentData(currentData: CurrentData) {
    this.chargeDischargeCurrent = Utils.convertChargeDischargeCurrent(this.translate, currentData.allComponents[this.component.id + '/_PropertyCurrent']);
    this.propertyMode = currentData.allComponents[this.component.id + '/_PropertyMode'];
  }

  async presentModal() {
    if (!this.isInitialized) {
      return;
    }
    const modal = await this.modalController.create({
      component: ModalComponent,
      componentProps: {
        component: this.component,
      },
    });
    return await modal.present();
  }
}
