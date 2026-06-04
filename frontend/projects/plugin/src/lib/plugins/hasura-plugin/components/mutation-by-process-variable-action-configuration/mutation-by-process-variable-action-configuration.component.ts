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
import {FunctionConfigurationComponent} from "@valtimo/plugin";
import {EditorModel} from "@valtimo/components";
import {BehaviorSubject, combineLatest, Observable, Subscription, take} from "rxjs";
import {MutationByProcessVariableActionConfig} from "../../models";

@Component({
  standalone: false,
  selector: "valtimo-mutation-by-process-variable-action-configuration",
  templateUrl: "./mutation-by-process-variable-action-configuration.component.html",
})
export class MutationByProcessVariableActionConfigurationComponent implements FunctionConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<MutationByProcessVariableActionConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<MutationByProcessVariableActionConfig> = new EventEmitter<MutationByProcessVariableActionConfig>();

  readonly mutationModel$ = new BehaviorSubject<EditorModel>({value: "", language: "graphql"});
  readonly objectsVariableName$ = new BehaviorSubject<string>("");
  readonly resultProcessVariableName$ = new BehaviorSubject<string>("");

  private readonly mutation$ = new BehaviorSubject<string>("");
  private readonly valid$ = new BehaviorSubject<boolean>(false);
  private saveSubscription!: Subscription;
  private prefillSubscription!: Subscription;

  ngOnInit(): void {
    this.prefillSubscription = this.prefillConfiguration$?.pipe(take(1)).subscribe(config => {
      if (config) {
        this.mutation$.next(config.mutation ?? "");
        this.objectsVariableName$.next(config.objectsVariableName ?? "");
        this.resultProcessVariableName$.next(config.resultProcessVariableName ?? "");
        this.mutationModel$.next({value: config.mutation ?? "", language: "graphql"});
      }
      this.emitValid();
    });

    this.saveSubscription = this.save$?.subscribe(() => {
      combineLatest([this.mutation$, this.objectsVariableName$, this.resultProcessVariableName$, this.valid$])
        .pipe(take(1))
        .subscribe(([mutation, objectsVariableName, resultProcessVariableName, valid]) => {
          if (valid) {
            this.configuration.emit({
              mutation,
              objectsVariableName,
              resultProcessVariableName: resultProcessVariableName || undefined,
            });
          }
        });
    });
  }

  ngOnDestroy(): void {
    this.saveSubscription?.unsubscribe();
    this.prefillSubscription?.unsubscribe();
  }

  onMutationChange(value: string): void {
    this.mutation$.next(value);
    this.emitValid();
  }

  onObjectsVariableNameChange(value: string): void {
    this.objectsVariableName$.next(value);
    this.emitValid();
  }

  onResultVariableChange(value: string): void {
    this.resultProcessVariableName$.next(value);
  }

  private emitValid(): void {
    const valid = !!(this.mutation$.value?.trim() && this.objectsVariableName$.value?.trim());
    this.valid$.next(valid);
    this.valid.emit(valid);
  }
}
