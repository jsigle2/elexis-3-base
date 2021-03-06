package at.medevit.elexis.agenda.ui.view;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import at.medevit.elexis.agenda.ui.composite.ParallelComposite;
import at.medevit.elexis.agenda.ui.composite.SideBarComposite;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class ParallelView {
	
	private ParallelComposite composite;
	
	private SideBarComposite sideBar;
	
	public ParallelView(){}
	
	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz){
		if (IAppointment.class.equals(clazz)) {
			if (composite != null && !composite.isDisposed()) {
				composite.refetchEvents();
			}
		}
	}
	
	@PostConstruct
	public void createPartControl(MPart part, ESelectionService selectionService,
		EMenuService menuService, Composite parent, UISynchronize uiSynchronize){
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		sideBar = new SideBarComposite(container, true, SWT.NONE);
		sideBar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		composite = new ParallelComposite(part, selectionService, menuService, container, SWT.NONE,
			uiSynchronize);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		CoreUiUtil.injectServicesWithContext(composite);
		sideBar.setAgendaComposite(composite);
	}
	
	@Focus
	public void setFocus(){
		if (composite != null && !composite.isDisposed()) {
			composite.setFocus();
		}
	}
	
	public SideBarComposite getSideBarComposite(){
		return sideBar;
	}
}
