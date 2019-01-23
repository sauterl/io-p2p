package com.github.sauterl.iop2p.ui.components;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public interface ModifiableListHandler<T> {

  void onRemove(ModifiableListView.RemoveEvent<T> event);

  void onAdd(ModifiableListView.AddEvent<T> event);
}
