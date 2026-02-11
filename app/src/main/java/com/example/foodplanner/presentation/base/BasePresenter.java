package com.example.foodplanner.presentation.base;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public abstract class BasePresenter<V extends BaseView> {
    protected V view;
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void attachView(V view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        compositeDisposable.clear();
    }

    protected void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    protected boolean isViewAttached() {
        return view != null;
    }
}
