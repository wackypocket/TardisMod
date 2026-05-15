package tardis.common.integration.ae;

import tardis.common.tileents.ComponentTileEntity;
import tardis.common.tileents.components.ComponentGrid;
import tardis.common.tileents.components.DummyComponentAspect;
import tardis.common.tileents.components.ITardisComponent;

public class ComponentGridFactory {

    public static ITardisComponent create(ComponentTileEntity parent) {
        if (AEIntegration.isAE2Available()) {
            return new ComponentGrid(parent);
        } else {
            return new DummyComponentAspect(parent);
        }
    }
}
