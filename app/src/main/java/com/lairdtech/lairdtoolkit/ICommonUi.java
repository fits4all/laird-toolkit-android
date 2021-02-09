package com.lairdtech.lairdtoolkit;

public interface ICommonUi
{
	/**
	 * Binds the generic textViews, Buttons, Layouts and ListViews in the
	 * Managers.
	 */
	public void bindViews();

	/**
	 * used to set handlers and adapters
	 */
	public void setAdapters();

	/**
	 * used to set listeners
	 */
	public void setListeners();

	public void uiInvalidateBtnState();
}