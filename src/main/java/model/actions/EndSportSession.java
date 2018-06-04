package model.actions;

import model.Database;
import model.Database.Collections;
import model.entities.ElectricProduction;
import model.entities.Module;
import model.entities.SportSession;
import model.misc.Runnable.new_Event;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class EndSportSession {
    public static void end(SportSession sportSession) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (sportSession != null) {
        	sportSession.setField(SportSession.Field.DURATION, new Date().getTime() - (Long) sportSession.getField(SportSession.Field.CREATION_DATE));
            ElectricProduction electricProduction = (ElectricProduction) Database.find_entity(
                    Database.Collections.ElectricProductions,
                    and(
                            eq(ElectricProduction.Field.MODULE_ID.get_key(), sportSession.getField(SportSession.Field.MODULE_ID)),
                            eq(ElectricProduction.Field.USER_ID.get_key(), sportSession.getField(SportSession.Field.USER_ID))
                    )
            );
            if (electricProduction == null) {
                electricProduction = (ElectricProduction) Database.new_entity(Database.Collections.ElectricProductions);
                electricProduction.setField(ElectricProduction.Field.MODULE_ID, sportSession.getField(SportSession.Field.MODULE_ID));
                electricProduction.setField(ElectricProduction.Field.USER_ID, sportSession.getField(SportSession.Field.USER_ID));
            }
            Module module = (Module) Database.find_entity(Database.Collections.Modules, Module.Field.ID, sportSession.getField(SportSession.Field.MODULE_ID));
            if (module != null) {
                module.setField(Module.Field.NEED_NEW_SESSION_ID, true);
                Database.update_entity(Database.Collections.Modules, module);
            }
            electricProduction.addProduction(sportSession.getField(SportSession.Field.PRODUCTION));
            Database.update_entity(Database.Collections.ElectricProductions, electricProduction);
            Database.insert_entity(Collections.SportSessions_HISTORY, sportSession);
            Database.delete_entity(Database.Collections.SportSessions, SportSession.Field.ID, sportSession.getField(SportSession.Field.ID));
        }
}
}
