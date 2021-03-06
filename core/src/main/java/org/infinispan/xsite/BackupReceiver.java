package org.infinispan.xsite;

import java.util.concurrent.CompletionStage;

import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.irac.IracUpdateKeyCommand;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.infinispan.metadata.Metadata;
import org.infinispan.metadata.impl.IracMetadata;
import org.infinispan.xsite.commands.XSiteStateTransferFinishReceiveCommand;
import org.infinispan.xsite.commands.XSiteStateTransferStartReceiveCommand;
import org.infinispan.xsite.statetransfer.XSiteStatePushCommand;

/**
 * Component present on a backup site that manages the backup information and logic.
 *
 * @see ClusteredCacheBackupReceiver
 * @author Mircea Markus
 * @since 5.2
 */
@Scope(Scopes.NAMED_CACHE)
public interface BackupReceiver {

   CompletionStage<Void> handleRemoteCommand(VisitableCommand command, boolean preserveOrder);

   /**
    * Updates the key with the value from a remote site.
    * <p>
    * If a conflict occurs, the update can be discarded.
    *
    * @param key          The key to update.
    * @param value        The new value.
    * @param metadata     The new {@link Metadata}.
    * @param iracMetadata The {@link IracMetadata} for conflict resolution.
    * @return A {@link CompletionStage} that is completed when the update is apply in the cluster or is discarded.
    */
   CompletionStage<Void> putKeyValue(Object key, Object value, Metadata metadata, IracMetadata iracMetadata);

   /**
    * Deletes the key.
    * <p>
    * This is a request from the remote site and the removal can be discarded if a conflict happens.
    *
    * @param key          The key to delete.
    * @param iracMetadata The {@link IracMetadata} for conflict resolution.
    * @return A {@link CompletionStage} that is completed when the key is deleted or it is discarded.
    */
   CompletionStage<Void> removeKey(Object key, IracMetadata iracMetadata);

   /**
    * Clears the cache.
    * <p>
    * This is not safe and it doesn't perform any conflict resolution.
    *
    * @return A {@link CompletionStage} that is completed when the cache is cleared.
    */
   CompletionStage<Void> clearKeys();

   /**
    * Forwards the {@link IracUpdateKeyCommand} to the primary owner.
    *
    * @param command The {@link IracUpdateKeyCommand} to forward.
    * @return A {@link CompletionStage} that is completed when the primary owner completes the request.
    */
   CompletionStage<Void> forwardToPrimary(IracUpdateKeyCommand command);

   /**
    * It handles starting the state transfer from a remote site. The command must be broadcast to the entire cluster in
    * which the cache exists.
    */
   CompletionStage<Void> handleStartReceivingStateTransfer(XSiteStateTransferStartReceiveCommand command);

   /**
    * It handles finishing the state transfer from a remote site. The command must be broadcast to the entire cluster in
    * which the cache exists.
    */
   CompletionStage<Void> handleEndReceivingStateTransfer(XSiteStateTransferFinishReceiveCommand command);

   /**
    * It handles the state transfer state from a remote site. It is possible to have a single node applying the state or
    * forward the state to respective primary owners.
    */
   CompletionStage<Void> handleStateTransferState(XSiteStatePushCommand cmd);
}
