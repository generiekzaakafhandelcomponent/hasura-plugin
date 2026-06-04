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
import {GraphQlByInputActionConfig} from "../../models";

@Component({
  standalone: false,
  selector: "valtimo-graphql-by-input-action-configuration",
  templateUrl: "./graphql-by-input-action-configuration.component.html",
})
export class GraphQlByInputActionConfigurationComponent implements FunctionConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<GraphQlByInputActionConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<GraphQlByInputActionConfig> = new EventEmitter<GraphQlByInputActionConfig>();

  readonly queryModel$ = new BehaviorSubject<EditorModel>({value: "", language: "graphql"});
  readonly variablesModel$ = new BehaviorSubject<EditorModel>({value: "", language: "json"});
  readonly resultProcessVariableName$ = new BehaviorSubject<string>("");

  private readonly query$ = new BehaviorSubject<string>("");
  private readonly variables$ = new BehaviorSubject<string>("");
  private readonly valid$ = new BehaviorSubject<boolean>(false);
  private saveSubscription!: Subscription;
  private prefillSubscription!: Subscription;

  ngOnInit(): void {
    this.prefillSubscription = this.prefillConfiguration$?.pipe(take(1)).subscribe(config => {
      if (config) {
        this.query$.next(config.query ?? "");
        this.variables$.next(config.variables ?? "");
        this.resultProcessVariableName$.next(config.resultProcessVariableName ?? "");
        this.queryModel$.next({value: config.query ?? "", language: "graphql"});
        this.variablesModel$.next({value: config.variables ?? "", language: "json"});
      }
      this.emitValid();
    });

    this.saveSubscription = this.save$?.subscribe(() => {
      combineLatest([this.query$, this.variables$, this.resultProcessVariableName$, this.valid$])
        .pipe(take(1))
        .subscribe(([query, variables, resultProcessVariableName, valid]) => {
          if (valid) {
            this.configuration.emit({
              query,
              variables: variables || undefined,
              resultProcessVariableName,
            });
          }
        });
    });
  }

  ngOnDestroy(): void {
    this.saveSubscription?.unsubscribe();
    this.prefillSubscription?.unsubscribe();
  }

  onQueryChange(value: string): void {
    this.query$.next(value);
    this.emitValid();
  }

  onVariablesChange(value: string): void {
    this.variables$.next(value);
  }

  onResultVariableChange(value: string): void {
    this.resultProcessVariableName$.next(value);
    this.emitValid();
  }

  private emitValid(): void {
    const valid = !!(this.query$.value?.trim() && this.resultProcessVariableName$.value?.trim());
    this.valid$.next(valid);
    this.valid.emit(valid);
  }
}
