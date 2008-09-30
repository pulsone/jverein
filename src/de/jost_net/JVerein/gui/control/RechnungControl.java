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
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.RechnungDetailAction;
import de.jost_net.JVerein.gui.input.FormularArtInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.ZahlungswegInput;
import de.jost_net.JVerein.gui.menu.RechungMenu;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.rmi.Abrechnung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.Dateiname;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungControl extends AbstractControl
{
  private de.willuhn.jameica.system.Settings settings;

  private DateInput datum = null;

  private Input zweck1;

  private Input zweck2;

  private DecimalInput betrag;

  private Abrechnung abr;

  private TablePart abrechnungsList;

  private DateInput vondatum = null;

  private DateInput bisdatum = null;

  private FormularInput formular = null;

  private FormularAufbereitung fa;

  public RechnungControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Abrechnung getAbrechnung()
  {
    if (abr != null)
    {
      return abr;
    }
    abr = (Abrechnung) getCurrentObject();
    return abr;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }

    Date d = getAbrechnung().getDatum();

    this.datum = new DateInput(d, Einstellungen.DATEFORMAT);
    this.datum.setTitle("Datum");
    this.datum.setText("Bitte Datum w�hlen");
    this.datum.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Date date = (Date) datum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return datum;
  }

  public Input getZweck1() throws RemoteException
  {
    if (zweck1 != null)
    {
      return zweck1;
    }
    zweck1 = new TextInput(getAbrechnung().getZweck1(), 27);
    zweck1.setMandatory(true);
    return zweck1;
  }

  public Input getZweck2() throws RemoteException
  {
    if (zweck2 != null)
    {
      return zweck2;
    }
    zweck2 = new TextInput(getAbrechnung().getZweck2(), 27);
    return zweck2;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getAbrechnung().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setMandatory(true);
    return betrag;
  }

  public DateInput getVondatum() throws RemoteException
  {
    if (vondatum != null)
    {
      return vondatum;
    }
    Date d = null;
    this.vondatum = new DateInput(d, Einstellungen.DATEFORMAT);
    this.vondatum.setTitle("Anfangsdatum");
    this.vondatum.setText("Bitte Anfangsdatum w�hlen");
    this.vondatum.setEnabled(false);
    this.vondatum.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Date date = (Date) vondatum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return vondatum;
  }

  public DateInput getBisdatum() throws RemoteException
  {
    if (bisdatum != null)
    {
      return bisdatum;
    }
    Date d = null;
    this.bisdatum = new DateInput(d, Einstellungen.DATEFORMAT);
    this.bisdatum.setTitle("Endedatum");
    this.bisdatum.setText("Bitte Endedatum w�hlen");
    this.bisdatum.setEnabled(false);
    this.bisdatum.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Date date = (Date) bisdatum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return bisdatum;
  }

  public FormularInput getFormular() throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    formular = new FormularInput(FormularArtInput.RECHNUNG);
    return formular;
  }

  public void handleStore()
  {
    try
    {
      Abrechnung a = getAbrechnung();
      a.setBetrag((Double) getBetrag().getValue());
      a.setDatum((Date) getDatum().getValue());
      a.setZweck1((String) getZweck1().getValue());
      a.setZweck2((String) getZweck2().getValue());
      a.store();
      GUI.getStatusBar().setSuccessText("Satz gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getView().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getAbrechungList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator abrechnungen = service.createList(Abrechnung.class);
    abrechnungen.setOrder("ORDER BY datum DESC");

    if (abrechnungsList == null)
    {
      abrechnungsList = new TablePart(abrechnungen,
          new RechnungDetailAction());
      abrechnungsList.addColumn("Name", "mitglied", new Formatter()
      {
        public String format(Object o)
        {
          Mitglied m = (Mitglied) o;
          if (m == null)
          {
            return null;
          }
          String name = null;
          try
          {
            name = m.getNameVorname();
          }
          catch (RemoteException e)
          {
            e.printStackTrace();
          }
          return name;
        }
      });
      abrechnungsList.addColumn("Datum", "datum", new DateFormatter(
          Einstellungen.DATEFORMAT));
      abrechnungsList.addColumn("Zweck1", "zweck1");
      abrechnungsList.addColumn("Zweck2", "zweck2");
      abrechnungsList.addColumn("Betrag", "betrag", new CurrencyFormatter("",
          Einstellungen.DECIMALFORMAT));
      abrechnungsList.setContextMenu(new RechungMenu());
      abrechnungsList.setRememberColWidths(true);
      abrechnungsList.setRememberOrder(true);
      abrechnungsList.setMulti(true);
      abrechnungsList.setSummary(true);
    }
    else
    {
      abrechnungsList.removeAll();
      while (abrechnungen.hasNext())
      {
        abrechnungsList.addItem((Abrechnung) abrechnungen.next());
      }
    }
    return abrechnungsList;
  }

  public Button getStartButton(final Object currentObject)
  {
    Button button = new Button("starten", new Action()
    {
      public void handleAction(Object context)
      {

        try
        {
          generiereRechnung(currentObject);
        }
        catch (RemoteException e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
        catch (IOException e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true);
    return button;
  }

  private void generiereRechnung(Object currentObject) throws IOException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    System.out.println(currentObject);
    fd.setText("Ausgabedatei w�hlen.");
    String path = settings
        .getString("lastdir", System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("rechnung", "", Einstellungen
        .getDateinamenmuster(), "PDF").get());
    fd.setFilterExtensions(new String[] { "*.PDF" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.endsWith(".PDF"))
    {
      s = s + ".PDF";
    }
    final File file = new File(s);
    Formular form = (Formular) getFormular().getValue();
    Formular fo = (Formular) Einstellungen.getDBService().createObject(
        Formular.class, form.getID());
    fa = new FormularAufbereitung(file);
    if (currentObject instanceof Abrechnung)
    {
      Abrechnung abr = (Abrechnung) currentObject;
      aufbereitenFormular(abr, fo, file);
    }
    if (currentObject instanceof Abrechnung[])
    {
      Abrechnung[] abrechnung = (Abrechnung[]) currentObject;
      for (Abrechnung abr : abrechnung)
      {
        aufbereitenFormular(abr, fo, file);
      }
    }
    fa.showFormular();

  }

  private void aufbereitenFormular(Abrechnung abr, Formular fo, File file)
      throws RemoteException
  {
    HashMap<String, Object> map = new HashMap<String, Object>();

    String empfaenger = abr.getMitglied().getAnrede() + "\n"
        + abr.getMitglied().getVornameName() + "\n"
        + abr.getMitglied().getStrasse() + "\n" + abr.getMitglied().getPlz()
        + " " + abr.getMitglied().getOrt();
    map.put(FormularfeldControl.EMPFAENGER, empfaenger);
    map.put(FormularfeldControl.ZAHLUNGSGRUND1, abr.getZweck1());
    map.put(FormularfeldControl.ZAHLUNGSGRUND2, abr.getZweck2());
    map.put(FormularfeldControl.BETRAG, abr.getBetrag());
    String zahlungsweg = "";
    switch (abr.getMitglied().getZahlungsweg())
    {
      case ZahlungswegInput.ABBUCHUNG:
      {
        zahlungsweg = "Abbuchung von Konto " + abr.getMitglied().getKonto()
            + ", BLZ: " + abr.getMitglied().getBlz();
        break;
      }
      case ZahlungswegInput.BARZAHLUNG:
      {
        zahlungsweg = "Bar";
        break;
      }
      case ZahlungswegInput.�BERWEISUNG:
      {
        zahlungsweg = "�berweisung";
        break;
      }
    }
    map.put(FormularfeldControl.ZAHLUNGSWEG, zahlungsweg);
    map.put(FormularfeldControl.TAGESDATUM, Einstellungen.DATEFORMAT
        .format(new Date()));
    // Date tmp = (Date) getBescheinigungsdatum().getValue();
    // String bescheinigungsdatum = Einstellungen.DATEFORMAT.format(tmp);
    // map.put("Bescheinigungsdatum", bescheinigungsdatum);
    // tmp = (Date) getSpendedatum().getValue();
    // String spendedatum = Einstellungen.DATEFORMAT.format(tmp);
    // map.put("Spendedatum", spendedatum);

    fa.writeForm(fo, map);
  }
}