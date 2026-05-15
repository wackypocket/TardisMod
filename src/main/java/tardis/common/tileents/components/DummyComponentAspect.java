package tardis.common.tileents.components;

import tardis.common.tileents.ComponentTileEntity;

public class DummyComponentAspect extends AbstractComponent {

    public DummyComponentAspect(ComponentTileEntity parent) {
        parentObj = parent;
    }

    @Override
    public ITardisComponent create(ComponentTileEntity parent) {
        return new DummyComponentAspect(parent);
    }

    // Add any other methods from ComponentAspect as no-op implementations if needed
}
