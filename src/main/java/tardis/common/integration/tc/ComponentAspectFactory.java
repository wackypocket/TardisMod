package tardis.common.integration.tc;

import tardis.common.tileents.ComponentTileEntity;
import tardis.common.tileents.components.ComponentAspect;
import tardis.common.tileents.components.DummyComponentAspect;
import tardis.common.tileents.components.ITardisComponent;

public class ComponentAspectFactory {

    public static ITardisComponent create(ComponentTileEntity parent) {
        if (ThaumcraftIntegration.isThaumcraftAvailable()) {
            return new ComponentAspect(parent);
        } else {
            return new DummyComponentAspect(parent);
        }
    }
}
