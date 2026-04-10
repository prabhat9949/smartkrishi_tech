package com.smartkrishi.presentation.disease;

import android.app.Application;
import com.smartkrishi.data.remote.ApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DiseaseViewModel_Factory implements Factory<DiseaseViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<ApiService> apiProvider;

  public DiseaseViewModel_Factory(Provider<Application> applicationProvider,
      Provider<ApiService> apiProvider) {
    this.applicationProvider = applicationProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public DiseaseViewModel get() {
    return newInstance(applicationProvider.get(), apiProvider.get());
  }

  public static DiseaseViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<ApiService> apiProvider) {
    return new DiseaseViewModel_Factory(applicationProvider, apiProvider);
  }

  public static DiseaseViewModel newInstance(Application application, ApiService api) {
    return new DiseaseViewModel(application, api);
  }
}
