/*
 * This file is part of the dSploit.
 *
 * Copyleft of Simone Margaritelli aka evilsocket <evilsocket@gmail.com>
 *
 * dSploit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dSploit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dSploit.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.evilsocket.dsploit.plugins;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import it.evilsocket.dsploit.R;
import it.evilsocket.dsploit.core.Plugin;
import it.evilsocket.dsploit.core.System;
import it.evilsocket.dsploit.net.Network;
import it.evilsocket.dsploit.net.Target;
import it.evilsocket.dsploit.tools.NMap;
import it.evilsocket.dsploit.tools.NMap.InspectionReceiver;

public class Inspector extends Plugin
{

	private ToggleButton mStartButton 	 = null;
	private ProgressBar	 mActivity    	 = null;
	private TextView     mDeviceType  	 = null;
	private TextView     mDeviceOS  	 = null;
	private TextView     mDeviceServices = null;
	private boolean      mRunning	  	 = false;
	private NMap         mNmap		  	 = null;
	private Receiver	 mReceiver	  	 = null;
	
	private class Receiver extends InspectionReceiver
	{
		@Override
		public void onServiceFound( final String service ) {
			Inspector.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	if( mDeviceServices.getText().equals("unknown") )
                		mDeviceServices.setText("");
                	
                	mDeviceServices.append( service + "\n" );
                }
            });			
		}

		@Override
		public void onOsFound( final String os ) {
			Inspector.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	mDeviceOS.setText( os );
                }
            });		
		}

		@Override
		public void onGuessOsFound( final String os ) {
			Inspector.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	mDeviceOS.setText( os );
                }
            });		
		}

		@Override
		public void onDeviceFound( final String device ) {
			Inspector.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	mDeviceType.setText( device );
                }
            });		
		}

		@Override
		public void onServiceInfoFound( final String info ) {
			Inspector.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	mDeviceOS.setText( info );
                }
            });		
		}
		
		@Override
		public void onEnd( int code ) {
			Inspector.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	if( mRunning )
                		setStoppedState();
                }
            });	
		}

		@Override
		public void onOpenPortFound( int port, String protocol ) {
			System.addOpenPort( port, Network.Protocol.fromString(protocol) );       				
		}
	}
	
	public Inspector() {
		super
		( 
		    "Inspector", 
		    "Perform target operating system and services deep detection ( slower than port scanner, but more accurate ).", 
		    new Target.Type[]{ Target.Type.ENDPOINT, Target.Type.REMOTE }, 
		    R.layout.plugin_inspector,
		    R.drawable.action_inspect_48 
		);
	}
	
	private void setStoppedState( ) {
		mNmap.kill();
		mActivity.setVisibility( View.INVISIBLE );
		mRunning = false;
		mStartButton.setChecked( false );                	
	}
	
	private void setStartedState( ) {
		mActivity.setVisibility( View.VISIBLE );
		mRunning = true;
		
		mNmap.inpsect( System.getTarget(), mReceiver ).start();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        
        mStartButton    = ( ToggleButton )findViewById( R.id.inspectToggleButton );
        mActivity	    = ( ProgressBar )findViewById( R.id.inspectActivity );
        mDeviceType 	= ( TextView)findViewById( R.id.deviceType ); 
        mDeviceOS   	= ( TextView)findViewById( R.id.deviceOS ); 
        mDeviceServices = ( TextView)findViewById( R.id.deviceServices ); 
        
        mStartButton.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				if( mRunning )
				{
					setStoppedState();
				}
				else
				{
					setStartedState();
				}
			}} 
		);
        
        mNmap 	  = new NMap( this );
        mReceiver = new Receiver();
	}
	@Override
	public void onBackPressed() {
		setStoppedState();	
	    super.onBackPressed();
	}
}
