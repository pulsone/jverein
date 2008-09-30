/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 * heiner@jverein.de
 * www.jverein.de
 * $Log$
 **********************************************************************/
package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.gui.action.SpendenbescheinigungDeleteAction;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu zu den Spendenbescheinigungen.
 */
public class SpendenbescheinigungMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Spendenbescheinigungen.
   */
  public SpendenbescheinigungMenu()
  {
    addItem(new CheckedContextMenuItem("L�schen...",
        new SpendenbescheinigungDeleteAction()));
  }
}