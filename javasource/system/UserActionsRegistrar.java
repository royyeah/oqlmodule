package system;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.mendix.core.Core;
import com.mendix.core.component.LocalComponent;
import com.mendix.core.component.MxRuntime;
import com.mendix.integration.Integration;

@Component(immediate = true, properties = {"event.topics:String=com/mendix/events/model/loaded"})
public class UserActionsRegistrar implements EventHandler
{
	private MxRuntime mxRuntime;
	private LocalComponent component;
	private Integration integration;
	
	@Reference
	public void setMxRuntime(MxRuntime runtime)
	{
		mxRuntime = runtime;
		mxRuntime.bundleComponentLoaded();
	}
	
	@Reference
	public void setIntegration(Integration integration)
	{
		this.integration = integration;
	}
	
	@Override
	public void handleEvent(Event event)
	{
		if (event.getTopic().equals(com.mendix.core.event.EventConstants.ModelLoadedTopic()))        
		{
			component = mxRuntime.getMainComponent();
			Core.initialize(component, integration);   
			component.actionRegistry().registerUserAction(oql.actions.AddBooleanParameter.class);
			component.actionRegistry().registerUserAction(oql.actions.AddDateTimeParameter.class);
			component.actionRegistry().registerUserAction(oql.actions.AddDecimalParameter.class);
			component.actionRegistry().registerUserAction(oql.actions.AddFloatValue.class);
			component.actionRegistry().registerUserAction(oql.actions.AddIntegerLongValue.class);
			component.actionRegistry().registerUserAction(oql.actions.AddObjectParameter.class);
			component.actionRegistry().registerUserAction(oql.actions.AddStringParameter.class);
			component.actionRegistry().registerUserAction(oql.actions.CountRowsOQLStatement.class);
			component.actionRegistry().registerUserAction(oql.actions.ExecuteOQLStatement.class);
			component.actionRegistry().registerUserAction(oql.actions.ExportOQLToCSV.class);
			component.actionRegistry().registerUserAction(system.actions.VerifyPassword.class);
		}
	}
}