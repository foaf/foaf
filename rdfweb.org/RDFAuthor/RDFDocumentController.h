/* RDFDocumentController */

/* $Id: RDFDocumentController.h,v 1.2 2002-04-10 15:22:20 pldms Exp $ */

/*
    Copyright 2002 Damian Steer <dm_steer@hotmail.com>

    This file is part of RDFAuthor.

    RDFAuthor is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RDFAuthor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RDFAuthor; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

#import <Cocoa/Cocoa.h>

@interface RDFDocumentController : NSDocumentController
{
    IBOutlet NSPanel *openUrlPanel;
    IBOutlet NSComboBox *urlComboBox;
}
- (IBAction)openUrl:(id)sender;
- (IBAction)urlSubmitted:(id)sender;
- (IBAction)userCancelled:(id)sender;
@end
