import { Component } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AbstractModal } from 'src/app/shared/genericComponents/modal/abstractModal';
import { ChannelAddress, CurrentData, Utils } from 'src/app/shared/shared';

@Component({
  templateUrl: './modal.html',
})
export class ModalComponent extends AbstractModal {

  public chargeDischargeCurrent: { name: string, value: number };

  public readonly CONVERT_WATT_TO_KILOWATT = Utils.CONVERT_WATT_TO_KILOWATT;
  public readonly CONVERT_MANUAL_ON_OFF = Utils.CONVERT_MANUAL_ON_OFF(this.translate);

  public chargeDischargePower: { name: string, value: number };

  protected override getChannelAddresses(): ChannelAddress[] {
    return [
      new ChannelAddress(this.component.id, "EssActivePowerSetPoint"),
    ];
  }

  protected override onCurrentData(currentData: CurrentData) {
    this.chargeDischargePower = Utils.convertChargeDischargePower(this.translate, currentData.allComponents[this.component.id + '/EssActivePowerSetPoint']);
  }

  protected override getFormGroup(): FormGroup {
    return this.formBuilder.group({
      mode: new FormControl(this.component.properties.mode),
      current: new FormControl(this.component.properties.current),
    });
  }
}
