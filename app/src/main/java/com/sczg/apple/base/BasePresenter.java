package com.sczg.apple.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BasePresenter<V extends BaseMvpView> {

    protected   V                     mView;
    private CompositeDisposable mCompositeDisposable;


    public void attach(V mView) {
        this.mView = mView;
    }

    public void detachView() {
        if (mView != null) {
            mView = null;
        }
    }

    protected V getView() {
        return mView;
    }

    public void addDisposable(Disposable d) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }
        this.mCompositeDisposable.add(d);
    }

    public void unDisposable() {

        if (this.mCompositeDisposable != null) {
            this.mCompositeDisposable.dispose();
        }
    }
}
