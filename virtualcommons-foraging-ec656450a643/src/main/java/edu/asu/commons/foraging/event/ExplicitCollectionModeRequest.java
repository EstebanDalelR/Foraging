package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;



/**
 * $Id$
 * 
 * Informs the server that the client's collection mode has changed.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class ExplicitCollectionModeRequest extends AbstractPersistableEvent {
    
    private static final long serialVersionUID = 5808499147598668770L;
    private final boolean explicitCollectionMode;
    
    public ExplicitCollectionModeRequest(Identifier id, boolean explicitCollectionMode) {
        super(id);
        this.explicitCollectionMode = explicitCollectionMode;
    }
    
    public boolean isExplicitCollectionMode() {
        return explicitCollectionMode;
    }
}
