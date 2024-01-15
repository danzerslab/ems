import { Component } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AbstractModal } from 'src/app/shared/genericComponents/modal/abstractModal';
import { ChannelAddress, CurrentData, Utils } from 'src/app/shared/shared';

@Component({
  templateUrl: './modal.html',
})
export class ModalComponent extends AbstractModal {

  public chargeDischargeCurrent: { name: string, value: number };

  public readonly CONVERT_TO_MILLIAMPERE = Utils.CONVERT_TO_MILLIAMPERE;
  public readonly CONVERT_MANUAL_ON_OFF = Utils.CONVERT_MANUAL_ON_OFF(this.translate);

  protected override getChannelAddresses(): ChannelAddress[] {
    return [
      new ChannelAddress(this.component.id, "_PropertyCurrent"),
    ];
  }

  protected override onCurrentData(currentData: CurrentData) {
    this.chargeDischargeCurrent = Utils.convertChargeDischargeCurrent(this.translate, currentData.allComponents[this.component.id + '/_PropertyCurrent']);
  }

  protected override getFormGroup(): FormGroup {
    return this.formBuilder.group({
      mode: new FormControl(this.component.properties.mode),
      current: new FormControl(this.component.properties.current),
    });
  }
}
