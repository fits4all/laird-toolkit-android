package com.lairdtech.lairdtoolkit;

public interface ICommonUi
{
	/**
	 * Binds the generic textViews, Buttons, Layouts and ListViews in the
	 * Managers.
	 */
	void bindViews();

	/**
	 * Used to set handlers and adapters.
	 */
	void setAdapters();

	/**
	 * Used to set listeners.
	 */
	void setListeners();

	/**
	 * Used to invalidate btn state.
	 */
	void uiInvalidateBtnState();
}