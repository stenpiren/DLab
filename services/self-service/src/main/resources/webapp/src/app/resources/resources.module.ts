/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '@angular/material';

import { ResourcesComponent } from './resources.component';
import { NavbarModule } from './../shared/navbar/index';
import { ModalModule } from './../shared/modal-dialog';
import { ResourcesGridModule } from './resources-grid';
import { ProgressDialogModule } from './../shared/modal-dialog/progress-dialog';
import { UploadKeyDialogModule } from './../shared/modal-dialog/key-upload-dialog';
import { ExploratoryEnvironmentCreateDialogModule } from './exploratory/exploratory-environment-create-dialog';

@NgModule({
  imports: [
    CommonModule,
    ModalModule,
    ResourcesGridModule,
    ProgressDialogModule,
    UploadKeyDialogModule,
    ExploratoryEnvironmentCreateDialogModule,
    NavbarModule,
    MaterialModule.forRoot()
  ],
  declarations: [ResourcesComponent],
  exports: [ResourcesComponent]
})
export class ResourcesModule { }