/*
 * Copyright 2026 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FunctionConfigurationComponent, FunctionConfigurationData} from "@valtimo/plugin";
import {EditorModel} from "@valtimo/components";
import {BehaviorSubject, combineLatest, Observable, Subscription, take} from "rxjs";
import {RunSqlActionConfig} from "../../models";

@Component({
  standalone: false,
  selector: "valtimo-run-sql-action-configuration",
  templateUrl: "./run-sql-action-configuration.component.html",
})
export class RunSqlActionConfigurationComponent implements FunctionConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<RunSqlActionConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<FunctionConfigurationData> = new EventEmitter<FunctionConfigurationData>();

  readonly sqlModel$ = new BehaviorSubject<EditorModel>({value: "", language: "sql"});

  private readonly sql$ = new BehaviorSubject<string>("");
  private readonly valid$ = new BehaviorSubject<boolean>(false);
  private saveSubscription!: Subscription;
  private prefillSubscription!: Subscription;

  ngOnInit(): void {
    this.prefillSubscription = this.prefillConfiguration$?.pipe(take(1)).subscribe(config => {
      if (config) {
        this.sql$.next(config.sql ?? "");
        this.sqlModel$.next({value: config.sql ?? "", language: "sql"});
      }
      this.emitValid();
    });

    this.saveSubscription = this.save$?.subscribe(() => {
      combineLatest([this.sql$, this.valid$])
        .pipe(take(1))
        .subscribe(([sql, valid]) => {
          if (valid) {
            this.configuration.emit({sql});
          }
        });
    });
  }

  ngOnDestroy(): void {
    this.saveSubscription?.unsubscribe();
    this.prefillSubscription?.unsubscribe();
  }

  onSqlChange(value: string): void {
    this.sql$.next(value);
    this.emitValid();
  }

  private emitValid(): void {
    const valid = !!this.sql$.value?.trim();
    this.valid$.next(valid);
    this.valid.emit(valid);
  }
}
