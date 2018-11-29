package model.actions;

import model.Database;
import model.Database.Collections;
import model.entities.ElectricProduction;
import model.entities.Module;
import model.entities.ModuleState;
import model.entities.SportSession;
import model.misc.Runnable.new_Event;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import Tools.LogManager;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class EndSportSession {
	public static void end(SportSession sportSession)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		LogManager.write("EndSportSession {");
		try {
			if (sportSession == null) {
				LogManager.write("No sport session found (sportSession==null).");
				return;
			}
			sportSession.setField(SportSession.Field.DURATION,
					new Date().getTime() - (Long) sportSession.getField(SportSession.Field.CREATION_DATE));
			ElectricProduction electricProduction = (ElectricProduction) Database.find_entity(
					Database.Collections.ElectricProductions,
					and(eq(ElectricProduction.Field.MODULE_ID.get_key(),
							sportSession.getField(SportSession.Field.MODULE_ID)),
							eq(ElectricProduction.Field.USER_ID.get_key(),
									sportSession.getField(SportSession.Field.USER_ID))));
			if (electricProduction == null) {
				LogManager.write("No electric production found (electricProduction==null), creating a new one.");
				electricProduction = (ElectricProduction) Database.new_entity(Database.Collections.ElectricProductions);
				electricProduction.setField(ElectricProduction.Field.MODULE_ID,
						sportSession.getField(SportSession.Field.MODULE_ID));
				electricProduction.setField(ElectricProduction.Field.USER_ID,
						sportSession.getField(SportSession.Field.USER_ID));
			}
			Module module = (Module) Database.find_entity(Database.Collections.Modules, Module.Field.ID,
					sportSession.getField(SportSession.Field.MODULE_ID));
			if (module != null) {
				module.setField(Module.Field.NEED_NEW_SESSION_ID, true);
                ModuleState moduleState = (ModuleState) Database.find_entity(Collections.ModuleStates, ModuleState.Field.CODE, 3);
                if (moduleState != null) {
                	module.setField(Module.Field.MODULE_STATE_ID, moduleState.getField(ModuleState.Field.ID));
                	module.setField(Module.Field.MODULE_STATE_CODE, moduleState.getField(ModuleState.Field.CODE));
                }
				Database.update_entity(Database.Collections.Modules, module);
			} else
				LogManager.write("Associated module not found (module==null).");

			LogManager.write("Adding production\n");
			electricProduction.addProduction(sportSession.getField(SportSession.Field.PRODUCTION));
			LogManager.write("production day=" + new Double((double) electricProduction.getField(ElectricProduction.Field.PRODUCTION_DAY))
					.toString());
			LogManager.write("production month=" + new Double((double) electricProduction.getField(ElectricProduction.Field.PRODUCTION_MONTH))
					.toString());
			LogManager.write("production year=" + new Double((double) electricProduction.getField(ElectricProduction.Field.PRODUCTION_YEAR))
					.toString());
			LogManager.write("production total=" + new Double((double) electricProduction.getField(ElectricProduction.Field.PRODUCTION_TOTAL))
					.toString());
			Database.update_entity(Database.Collections.ElectricProductions, electricProduction);
			Database.insert_entity(Collections.SportSessions_HISTORY, sportSession);
			Database.delete_entity(Database.Collections.SportSessions, SportSession.Field.ID,
					sportSession.getField(SportSession.Field.ID));
		} finally {
			LogManager.write("}");
		}
	}
}
