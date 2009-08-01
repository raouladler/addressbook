package de.rcpbuch.addressbook.editor.internal;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import de.ralfebert.rcputils.databinding.ModelDataBindingEditorPart;
import de.rcpbuch.addressbook.editor.AddressEditorConstants;
import de.rcpbuch.addressbook.editor.AddressIdEditorInput;
import de.rcpbuch.addressbook.entities.Address;
import de.rcpbuch.addressbook.services.IAddressService;

public class AddressEditorPart extends ModelDataBindingEditorPart<AddressIdEditorInput, Address> {

	private IAddressService addressService;

	private Text txtName;
	private Text txtStreet;
	private Text txtZip;
	private Text txtCity;
	private ComboViewer cvCountry;

	@Override
	public void onCreatePartControl(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, AddressEditorConstants.HELP_CONTEXT_EDIT);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		final ScrolledForm form = toolkit.createScrolledForm(parent);
		FillLayout layout = new FillLayout();
		form.getBody().setLayout(layout);

		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE | Section.TITLE_BAR);
		section.setText("Anschrift");

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayoutFactory.fillDefaults().margins(10, 3).numColumns(3).applyTo(client);
		section.setClient(client);

		// NAME
		toolkit.createLabel(client, "Name:");

		txtName = toolkit.createText(client, "");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(2, 1).applyTo(txtName);

		// STRASSE
		toolkit.createLabel(client, "Straße:");

		txtStreet = toolkit.createText(client, "");
		txtStreet.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(2, 1).applyTo(txtStreet);

		// PLZ / ORT
		toolkit.createLabel(client, "PLZ/Ort:");

		txtZip = toolkit.createText(client, "");
		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(txtZip);

		txtCity = toolkit.createText(client, "");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(txtCity);

		ControlDecoration decoration = new ControlDecoration(txtCity, SWT.RIGHT | SWT.TOP);
		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage();
		decoration.setImage(errorImage);

		new AutoCompleteField(txtCity, new TextContentAdapter(), addressService.getAllCities());

		// LAND
		toolkit.createLabel(client, "Land:");

		cvCountry = new ComboViewer(client, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(2, 1).applyTo(
				cvCountry.getCombo());
		cvCountry.setContentProvider(new ArrayContentProvider());
		cvCountry.setLabelProvider(new CountryLabelProvider());
		cvCountry.setInput(addressService.getAllCountries());

		section.setExpanded(true);

	}

	@Override
	protected Address onLoad(IEditorInput input) {
		return addressService.getAddress(getEditorInput().getId());
	}

	@Override
	protected void onBind(DataBindingContext ctx, IObservableValue model) {
		IObservableValue name = PojoObservables.observeDetailValue(model, "name", String.class);
		ctx.bindValue(SWTObservables.observeText(txtName, SWT.Modify), name);
		ctx.bindValue(getPartNameObservable(), name);
		ctx.bindValue(SWTObservables.observeText(txtStreet, SWT.Modify), PojoObservables.observeDetailValue(model,
				"street", String.class));
		ctx.bindValue(SWTObservables.observeText(txtZip, SWT.Modify), PojoObservables.observeDetailValue(model, "zip",
				String.class));
		ctx.bindValue(SWTObservables.observeText(txtCity, SWT.Modify), PojoObservables.observeDetailValue(model,
				"city", String.class));
		ctx.bindValue(ViewersObservables.observeSingleSelection(cvCountry), PojoObservables.observeDetailValue(model,
				"country", String.class));
	}

	@Override
	protected Address onSave(Address modelObject, IProgressMonitor monitor) {
		return addressService.saveAddress(modelObject);
	}

	@Override
	public void setFocus() {
		txtName.setFocus();
	}

	public void setAddressService(IAddressService addressService) {
		this.addressService = addressService;
	}

}